package com.druid.timepaint.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ArraysDeepCopyUtils<T> {
    public ArraysDeepCopyUtils() {

    }

    public List<T> deepCopy(List<T> srcList) {
        List<T> desList = new ArrayList<>();
        try {
            ByteArrayOutputStream baos = null;
            ObjectOutputStream oos = null;
            ByteArrayInputStream bais = null;
            ObjectInputStream ois = null;
            try {
                //序列化
                baos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(baos);
                oos.writeObject(srcList);

                bais = new ByteArrayInputStream(baos.toByteArray());
                ois = new ObjectInputStream(bais);
                desList = (List<T>) ois.readObject();
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                baos.close();
                oos.close();
                bais.close();
                ois.close();
            }
        } catch (Exception ex) {

        }
        return desList;
    }
}
