package cn.myafx.rabbitmq;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * mq配置
 */
public class MQConfig implements IMQConfig {
    private Map<String, ExchangeConfig> exchangeMap;
    private Map<String, QueueConfig> queueMap;
    private Map<String, PubConfig> pubMsgMap;
    private Map<String, SubConfig> subMsgMap;

    /**
     * 判断是否为null or ""
     * 
     * @param value 字符串
     * @return boolean
     */
    private boolean isNullOrEmpty(String value) {
        return value == null || value.length() == 0;
    }

    /**
     * mq配置
     * 
     * @param xmlFile xmlFile
     * @throws Exception Exception
     */
    public MQConfig(String xmlFile) throws Exception {
        if (isNullOrEmpty(xmlFile))
            throw new Exception("xmlFile is null!");
        File f = new File(xmlFile);
        if (!f.exists() || !f.isFile())
            throw new Exception("xmlFile(" + xmlFile + ") not found!");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(f);
        this.load(doc);
    }

    /**
     * mq配置
     * 
     * @param stream xmlFile
     * @throws Exception Exception
     */
    public MQConfig(InputStream stream) throws Exception {
        this.load(stream);
    }

    /**
     * mq配置
     * 
     * @param url url
     * @throws Exception Exception
     */
    public MQConfig(URL url) throws Exception {
        if (url == null)
            throw new Exception("url is null!");
        try (var stream = url.openStream()) {
            this.load(stream);
        }
    }

    private void load(InputStream stream) throws Exception {
        if (stream == null)
            throw new Exception("stream is null!");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(stream);
        this.load(doc);
    }

    private void load(Document doc) throws Exception {
        var rootElement = doc.getDocumentElement();
        var nodes = rootElement.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (!(n instanceof Element node))
                continue;
            switch (node.getNodeName()) {
                case "Exchange":
                case "ExchangeConfig":
                    this.loadExchangeConfig(node);
                    break;
                case "Queue":
                case "QueueConfig":
                    this.loadQueueConfig(node);
                    break;
                case "Pub":
                case "PubMsg":
                case "PubConfig":
                    this.loadPubConfig(node);
                    break;
                case "Sub":
                case "SubMsg":
                case "SubConfig":
                    this.loadSubConfig(node);
                    break;
            }
        }
    }

    private void loadExchangeConfig(Element rootElement) throws Exception {
        var nodes = rootElement.getChildNodes();
        exchangeMap = new HashMap<>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            Node in = nodes.item(i);
            if (!(in instanceof Element item))
                continue;
            if (item.getNodeName() != "Key")
                continue;
            var s = item.getAttribute("exchange");
            if (isNullOrEmpty(s))
                throw new Exception("Exchange config is null!");
            if (exchangeMap.containsKey(s))
                throw new Exception("Exchange config (" + s + ") is repeat！");
            var m = new ExchangeConfig();
            m.Exchange = s;
            s = item.getAttribute("type");
            if (!isNullOrEmpty(s))
                m.Type = s;
            s = item.getAttribute("durable");
            if (!isNullOrEmpty(s))
                m.Durable = s.toLowerCase().equals("true") || s.equals("1");
            s = item.getAttribute("autoDelete");
            if (!isNullOrEmpty(s))
                m.AutoDelete = s.toLowerCase().equals("true") || s.equals("1");

            var args = item.getChildNodes();
            for (int j = 0; j < args.getLength(); j++) {
                Node rnode = args.item(j);
                if (!(rnode instanceof Element rel))
                    continue;
                if (rel.getNodeName() != "Arguments")
                    continue;
                var k = rel.getAttribute("key");
                var v = rel.getAttribute("value");
                if (!isNullOrEmpty(k) && !isNullOrEmpty(v)) {
                    if (m.Arguments == null)
                        m.Arguments = new HashMap<>(args.getLength());
                    if (!m.Arguments.containsKey(k))
                        m.Arguments.put(k, v);
                }
            }

            exchangeMap.put(m.Exchange, m);
        }
    }

    private void loadQueueConfig(Element rootElement) throws Exception {
        var nodes = rootElement.getChildNodes();
        queueMap = new HashMap<>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            Node qn = nodes.item(i);
            if (!(qn instanceof Element item))
                continue;
            if (item.getNodeName() != "Key")
                continue;
            var s = item.getAttribute("queue");
            if (isNullOrEmpty(s))
                throw new Exception("queue config is null!");
            if (queueMap.containsKey(s))
                throw new Exception("queue config (" + s + ") is repeat!");
            var m = new QueueConfig();
            m.Queue = s;
            s = item.getAttribute("routingKey");
            if (!isNullOrEmpty(s))
                m.RoutingKey = s;

            s = item.getAttribute("delayQueue");
            if (!isNullOrEmpty(s)) {
                m.DelayQueue = s;
                s = item.getAttribute("delayRoutingKey");
                if (!isNullOrEmpty(s))
                    m.DelayRoutingKey = s;
            }

            s = item.getAttribute("durable");
            if (!isNullOrEmpty(s))
                m.Durable = s.toLowerCase().equals("true") || s.equals("1");
            s = item.getAttribute("exclusive");
            if (!isNullOrEmpty(s))
                m.Exclusive = s.toLowerCase().equals("true") || s.equals("1");
            s = item.getAttribute("autoDelete");
            if (!isNullOrEmpty(s))
                m.AutoDelete = s.toLowerCase().equals("true") || s.equals("1");

            s = item.getAttribute("isQueueParam");
            if (!isNullOrEmpty(s))
                m.IsQueueParam = s.toLowerCase().equals("true") || s.equals("1");

            s = item.getAttribute("isRoutingKeyParam");
            if (!isNullOrEmpty(s))
                m.IsRoutingKeyParam = s.toLowerCase().equals("true") || s.equals("1");

            var args = item.getChildNodes();
            for (int j = 0; j < args.getLength(); j++) {
                Node an = args.item(j);
                if (!(an instanceof Element rel))
                    continue;
                var n = rel.getNodeName();
                if (!("QueueArguments".equals(n) || "BindArguments".equals(n)))
                    continue;
                var k = rel.getAttribute("key");
                var v = rel.getAttribute("value");
                if (!isNullOrEmpty(k) && !isNullOrEmpty(v)) {
                    if ("QueueArguments".equals(n)) {
                        if (m.QueueArguments == null)
                            m.QueueArguments = new HashMap<>(args.getLength());
                        if (!m.QueueArguments.containsKey(k))
                            m.QueueArguments.put(k, v);
                    } else {
                        if (m.BindArguments == null)
                            m.BindArguments = new HashMap<>(args.getLength());
                        if (!m.BindArguments.containsKey(k))
                            m.BindArguments.put(k, v);
                    }
                }
            }

            queueMap.put(m.Queue, m);
        }
    }

    private void loadPubConfig(Element rootElement) throws Exception {
        var nodes = rootElement.getChildNodes();
        pubMsgMap = new HashMap<>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            Node in = nodes.item(i);
            if (!(in instanceof Element item))
                continue;
            if (item.getNodeName() != "Key")
                continue;
            var s = item.getAttribute("name");
            if (isNullOrEmpty(s))
                throw new Exception("PubMsg name config is null!");
            if (pubMsgMap.containsKey(s))
                throw new Exception("PubMsg name (" + s + ") is repeat！");
            var m = new PubConfig();
            m.Name = s;
            s = item.getAttribute("routingKey");
            if (!isNullOrEmpty(s))
                m.RoutingKey = s;
            s = item.getAttribute("delayRoutingKey");
            if (!isNullOrEmpty(s))
                m.DelayRoutingKey = s;
            s = item.getAttribute("exchange");
            if (!isNullOrEmpty(s))
                m.Exchange = s;
            s = item.getAttribute("isRoutingKeyParam");
            if (!isNullOrEmpty(s))
                m.IsRoutingKeyParam = s.toLowerCase().equals("true") || s.equals("1");

            pubMsgMap.put(m.Name, m);
        }
    }

    private void loadSubConfig(Element rootElement) throws Exception {
        var nodes = rootElement.getChildNodes();
        subMsgMap = new HashMap<>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            Node in = nodes.item(i);
            if (!(in instanceof Element item))
                continue;
            if (item.getNodeName() != "Key")
                continue;
            var s = item.getAttribute("name");
            if (isNullOrEmpty(s))
                throw new Exception("SubMsg name config is null!");
            if (subMsgMap.containsKey(s))
                throw new Exception("SubMsg name (" + s + ") is repeat!");
            var m = new SubConfig();
            m.Name = s;
            s = item.getAttribute("queue");
            if (isNullOrEmpty(s))
                throw new Exception("SubMsg queue config is null!");
            m.Queue = s;
            s = item.getAttribute("isQueueParam");
            if (!isNullOrEmpty(s))
                m.IsQueueParam = s.toLowerCase().equals("true") || s.equals("1");

            subMsgMap.put(m.Name, m);
        }
    }

    /**
     * 获取配置交换器
     * 
     * @return ExchangeConfig
     */
    @Override
    public List<ExchangeConfig> getExchanges() {
        if (this.exchangeMap == null)
            return null;
        var list = new ArrayList<ExchangeConfig>(this.exchangeMap.size());
        this.exchangeMap.forEach((k, v) -> {
            list.add(v);
        });
        return list;
    }

    /**
     * 获取配置队列
     * 
     * @return QueueConfig
     */
    @Override
    public List<QueueConfig> getQueues() {
        if (this.queueMap == null)
            return null;
        var list = new ArrayList<QueueConfig>(this.queueMap.size());
        this.queueMap.forEach((k, v) -> {
            list.add(v);
        });
        return list;
    }

    /**
     * 获取配置生产者
     * 
     * @param name 节点名称
     * @return PubMsgConfig
     */
    @Override
    public PubConfig getPubConfig(String name) {
        if (this.pubMsgMap == null)
            return null;
        var pc = this.pubMsgMap.get(name);
        return pc != null ? pc.copy() : null;
    }

    /**
     * 获取配置消费者
     * 
     * @param name 节点名称
     * @return SubMsgConfig
     */
    @Override
    public SubConfig getSubConfig(String name) {
        if (this.subMsgMap == null)
            return null;
        var sc = this.subMsgMap.get(name);
        return sc != null ? sc.copy() : null;
    }

    /**
     * close
     */
    @Override
    public void close() throws Exception {
        this.exchangeMap = null;
        this.queueMap = null;
        this.pubMsgMap = null;
        this.subMsgMap = null;
    }
}
