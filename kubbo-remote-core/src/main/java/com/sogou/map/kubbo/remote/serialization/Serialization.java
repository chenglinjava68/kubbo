package com.sogou.map.kubbo.remote.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sogou.map.kubbo.common.extension.SPI;
import com.sogou.map.kubbo.remote.serialization.hessian.HessianSerialization;

/**
 * Serialization.
 * 
 * @author liufuliang
 */
@SPI(HessianSerialization.NAME)
public interface Serialization {

    /**
     * get content type id
     * 
     * @return content type id
     */
    byte getContentTypeId();

    /**
     * get content type
     * 
     * @return content type
     */
    String getContentType();

    /**
     * create serializer
     * @param output 输出流
     * @return 序列化对象输出
     * @throws IOException
     */
    ObjectOutput serialize(OutputStream output) throws IOException;

    /**
     * create deserializer
     * @param input 输入流
     * @return 反序列化器输入
     * @throws IOException
     */
    ObjectInput deserialize(InputStream input) throws IOException;


}