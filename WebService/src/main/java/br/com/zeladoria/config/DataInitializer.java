package br.com.zeladoria.config;

import br.com.zeladoria.model.CategoriaProblema;
import br.com.zeladoria.repository.CategoriaProblemaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner inicializarCategoriasPadrao(CategoriaProblemaRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                List<CategoriaProblema> categoriasPadrao = List.of(
                        new CategoriaProblema("Buraco em Logradouro", "Buracos, crateras ou afundamentos no asfalto ou calçamento de vias públicas."),
                        new CategoriaProblema("Iluminação Pública", "Lâmpadas queimadas, piscando, acesas durante o dia ou postes sem iluminação."),
                        new CategoriaProblema("Coleta de Lixo e Entulho", "Acúmulo irregular de lixo domiciliar, entulho de obras ou descarte clandestino em vias públicas."),
                        new CategoriaProblema("Poda de Árvore e Jardinagem", "Árvores com risco de queda, galhos obstruindo fiação/sinalização ou mato alto em praças e canteiros."),
                        new CategoriaProblema("Sinalização Viária", "Placas de trânsito danificadas, semáforos apagados ou faixas de pedestres apagadas.")
                );
                repository.saveAll(categoriasPadrao);
            }
        };
    }
}
