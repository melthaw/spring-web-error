package in.clouthink.daas.we.sample.web;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class Foo implements Serializable {
    
    @NotNull
    private String name;
    
    public Foo() {
    }
    
    public Foo(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
}
