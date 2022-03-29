package demo.eureka.client.provider

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@EnableDiscoveryClient
@SpringBootApplication
class ClientProviderApplication

fun main(args: Array<String>) {
    runApplication<ClientProviderApplication>(*args)
}
