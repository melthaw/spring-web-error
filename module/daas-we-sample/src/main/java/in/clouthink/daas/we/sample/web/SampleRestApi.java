package in.clouthink.daas.we.sample.web;

import in.clouthink.daas.we.ApplicationException;
import in.clouthink.daas.we.SharedErrorCode;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/we/sample")
public class SampleRestApi {
    
    @RequestMapping(value = "/err1", method = RequestMethod.GET)
    @ResponseBody
    public void err1() {
        throw new RuntimeException("Hello error world");
    }
    
    @RequestMapping(value = "/err2", method = RequestMethod.GET)
    @ResponseBody
    public void err2() {
        throw new ApplicationException(SharedErrorCode.NOT_FOUND);
    }
    
    @RequestMapping(value = "/err3", method = RequestMethod.GET)
    @ResponseBody
    public void err3(@Valid Foo foo) {
    }
    
    @RequestMapping(value = "/err4", method = RequestMethod.GET)
    @ResponseBody
    public void err4() {
        throw new org.springframework.security.access.AccessDeniedException("The username or password is invalid");
    }
    
    @RequestMapping(value = "/err5", method = RequestMethod.GET)
    @ResponseBody
    public void err5() {
        throw new ApplicationException(SharedErrorCode.BAD_REQUEST,
                                       "The error message is customized");
    }
    
}
