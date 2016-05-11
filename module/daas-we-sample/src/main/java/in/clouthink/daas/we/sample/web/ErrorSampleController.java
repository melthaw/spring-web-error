package in.clouthink.daas.we.sample.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/we/sample2")
public class ErrorSampleController {

	@RequestMapping(value = "/err1", method = RequestMethod.GET)
	public String errorPage1() {
		throw new RuntimeException("error 1");
	}

	@RequestMapping(value = "/err2", method = RequestMethod.GET)
	@ResponseBody
	public String errorPage2() {
		throw new RuntimeException("error 2");
	}

}
