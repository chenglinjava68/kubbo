package com.sogou.map.kubbo.remote.serialization.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sogou.map.kubbo.common.URL;
import com.sogou.map.kubbo.remote.serialization.ObjectInput;
import com.sogou.map.kubbo.remote.serialization.ObjectOutput;
import com.sogou.map.kubbo.remote.serialization.Serialization;

public class JavaSerialization implements Serialization  {

    public static final String NAME = "java";

    public byte getContentTypeId() {
        return 1;
    }

    public String getContentType() {
        return "x-application/java";
    }

    public ObjectOutput serialize(URL url, OutputStream output) throws IOException {
        return new JavaObjectOutput(output);
    }

    public ObjectInput deserialize(URL url, InputStream input) throws IOException {
        return new JavaObjectInput(input);
    }
}
