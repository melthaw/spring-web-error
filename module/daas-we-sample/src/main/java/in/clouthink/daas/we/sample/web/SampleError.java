package in.clouthink.daas.we.sample.web;

import in.clouthink.daas.we.ErrorCodeProvider;

/**
 *
 */
public enum SampleError implements ErrorCodeProvider {
    
    ERR1(Constants.ERR1), ERR2(Constants.ERR2), ERR3(Constants.ERR3);
    
    private String value;
    
    SampleError(String value) {
        this.value = value;
    }
    
    @Override
    public String getErrorCode() {
        return value;
    }
    
	static interface Constants {
        static final String ERR1 = "10001";
        
        static final String ERR2 = "10002";
        
        static final String ERR3 = "10003";
    }
    
}
