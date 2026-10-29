package br.com.zeladoria.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Zeladoria Urbana")
                        .version("1.0.0")
                        .description("Webservice RESTful para relato e acompanhamento de problemas de zeladoria urbana (buracos em logradouros, iluminação pública, coleta de lixo, etc.), com controle de geração de IDs pelo JPA e exclusão estritamente lógica.")
                        .contact(new Contact()
                                .name("Equipe de Zeladoria Urbana")
                                .email("contato@zeladoria.gov.br"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Servidor de Desenvolvimento Local")
                ));
    }
}
