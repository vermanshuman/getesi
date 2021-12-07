package it.nexera.ris.common.utils;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class CustomClassLoader extends URLClassLoader{
    public CustomClassLoader(URL[] urls ) {
        super(urls, null);
      }
    
    @Override
    protected Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException {
      try {
        return super.loadClass(name, resolve);
      } catch (ClassNotFoundException e) {
        return Class.forName(name, resolve, CustomClassLoader.class.getClassLoader());
      }
    }

    @Override
    protected void finalize() throws Throwable {
      super.finalize();
    }
    
    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub
        super.close();
    }

}
