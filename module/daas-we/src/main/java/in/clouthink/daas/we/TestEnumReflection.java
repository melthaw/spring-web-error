package in.clouthink.daas.we;

import java.lang.reflect.Field;

/**
 *
 */
public class TestEnumReflection {
    
    enum Color {
        
        RED, BLACK, WHITE;
        
        static final Color red = Color.RED;
        
    }
    
    public static void main(String[] args) {
        for (Field field : Color.class.getDeclaredFields()) {
            if (field.isEnumConstant()) {
                System.out.println(field.getName());
            }
        }
    }
    
}
