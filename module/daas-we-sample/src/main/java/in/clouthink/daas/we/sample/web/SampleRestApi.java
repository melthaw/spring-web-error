package in.clouthink.daas.we.sample.web;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import in.clouthink.daas.we.ApplicationException;
import in.clouthink.daas.we.SharedErrorCode;
import in.clouthink.daas.we.annotation.ErrorMapping;
import in.clouthink.daas.we.annotation.ErrorMappings;

@RestController
@RequestMapping("/we/sample")
public class SampleRestApi {
    
    /**
     * unmatched error mapping , case 1 : nothing provided by the exception
     */
    @ErrorMappings(value = { @ErrorMapping(errorCode = SampleError.Constants.ERR1, httpStatus = HttpStatus.BAD_REQUEST),
                             @ErrorMapping(errorCode = SampleError.Constants.ERR2, httpStatus = HttpStatus.BAD_REQUEST) })
    @RequestMapping(value = "/err1", method = RequestMethod.GET)
    @ResponseBody
    public void err1() {
        throw new RuntimeException("Hello error world");
    }
    
    /**
     * convert the framework exception the exception
     */
    @RequestMapping(value = "/err2", method = RequestMethod.GET)
    @ResponseBody
    public void err2(@Valid Foo foo) {
    }
    
    /**
     * matched error mapping
     */
    @ErrorMappings(value = { @ErrorMapping(errorCode = SampleError.Constants.ERR3, httpStatus = HttpStatus.BAD_REQUEST) })
    @RequestMapping(value = "/err3", method = RequestMethod.GET)
    @ResponseBody
    public void err3() {
        throw new ApplicationException(SampleError.ERR3, "oh ,foo......");
    }
    
    /**
     * auto scan the error type
     */
    @ErrorMappings(httpStatus = HttpStatus.INTERNAL_SERVER_ERROR, errorType = { SampleError.class })
    @RequestMapping(value = "/err4", method = RequestMethod.GET)
    @ResponseBody
    public void err4() {
        throw new ApplicationException(SampleError.ERR3, "oh ,foo......");
    }
    
    /**
     * catch and convert the framework exception
     */
    @RequestMapping(value = "/err5", method = RequestMethod.GET)
    @ResponseBody
    public void err5() {
        throw new org.springframework.security.access.AccessDeniedException("The username or password is invalid");
    }
    
    /**
     * catch and resolve the application exception
     */
    @RequestMapping(value = "/err6", method = RequestMethod.GET)
    @ResponseBody
    public void err6() {
        throw new ApplicationException(SharedErrorCode.BAD_REQUEST,
                                       "The error message is customized");
    }
    
}
