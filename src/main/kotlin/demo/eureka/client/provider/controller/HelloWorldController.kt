package demo.eureka.client.provider.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class HelloWorldController {

    @GetMapping("/hello")
    fun hello():Map<String,String>{
        return mapOf("data" to "hello world")
    }


}