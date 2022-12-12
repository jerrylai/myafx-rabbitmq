package cn.myafx.rabbitmq;

/**
 * MapperDto
 */
public class MapperDto {
    /**
     * contentType
     */
    public String contentType;
    /**
     * utf-8
     */
    public String contentEncoding = "utf-8";
    /**
     * 
     */
    public byte[] body;

    /**
     * MapperDto
     */
    public MapperDto() {
    }

    /**
     * MapperDto
     * 
     * @param contentType contentType
     * @param body        body
     */
    public MapperDto(String contentType, byte[] body) {
        this.contentType = contentType;
        this.body = body;
    }
}
