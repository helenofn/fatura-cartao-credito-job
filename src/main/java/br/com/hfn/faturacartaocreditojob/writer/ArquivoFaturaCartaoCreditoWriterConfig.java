package br.com.hfn.faturacartaocreditojob.writer;

import java.io.IOException;
import java.io.Writer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemWriter;
import org.springframework.batch.item.file.ResourceSuffixCreator;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemWriterBuilder;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import br.com.hfn.faturacartaocreditojob.dominio.FaturaCartaoCredito;
import br.com.hfn.faturacartaocreditojob.dominio.Transacao;

@Configuration
public class ArquivoFaturaCartaoCreditoWriterConfig {

	@Bean
	public MultiResourceItemWriter<FaturaCartaoCredito> arquivosFaturaCartaoCredito(){
		return new MultiResourceItemWriterBuilder<FaturaCartaoCredito>()
				.name("arquivoFaturaCartaoCredito")
				.resource(new FileSystemResource("files/fatura"))
				.itemCountLimitPerResource(1)
				.resourceSuffixCreator(sifficCreator())
				.delegate(arquivoFaturaCartaoCredito())
				.build();
	}

	private FlatFileItemWriter<FaturaCartaoCredito> arquivoFaturaCartaoCredito() {
		return new FlatFileItemWriterBuilder<FaturaCartaoCredito>()
				.name("arquivoFaturaCartaoCredito")
				.resource(new FileSystemResource("files/fatura.txt"))
				.lineAggregator(lineAggregator())
				.headerCallback(headerCallback())
				.footerCallback(footerCallback())
				.build();
	}

	@Bean
	public FlatFileFooterCallback footerCallback() {
		return new TotalTransacoesFooterCallback();
	}

	private FlatFileHeaderCallback headerCallback() {
		return new FlatFileHeaderCallback() {

			@Override
			public void writeHeader(Writer writer) throws IOException {
				writer.append(String.format("%121s\n", "Cartão XPTO"));
				writer.append(String.format("%121s\n\n", "Rua Vergeuro, 131"));
			}
			
		};
	}

	private LineAggregator<FaturaCartaoCredito> lineAggregator() {
		return new  LineAggregator<FaturaCartaoCredito>() {

			@Override
			public String aggregate(FaturaCartaoCredito item) {
				StringBuilder writer = new StringBuilder();
				writer.append(String.format("Nome: %s\n", item.getCliente().getNome()));
				writer.append(String.format("Endereço: %s\n\n\n", item.getCliente().getEndereco()));
				writer.append(String.format("Fatura completa do cartão: %d\n", item.getCartaoCredito().getNumeroCartaoCredito()));
				writer.append("------------------------------------------------------------------------------------------\n");
				writer.append("DATA DESCRICAO VALOR\n");
				writer.append("------------------------------------------------------------------------------------------\n");
				
				for (Transacao transacao : item.getTransacoes()) {
					writer.append(String.format("\n[%10s] %-80s - %s", 
							new SimpleDateFormat("dd/MM/yyyy").format(transacao.getData()),
							transacao.getDescricao(),
							NumberFormat.getCurrencyInstance().format(transacao.getValor())));
				}
				
				return writer.toString();
			}
			
		};
	}

	private ResourceSuffixCreator sifficCreator() {
		return new ResourceSuffixCreator() {

			@Override
			public String getSuffix(int index) {
				return index + ".txt";
			}
			
		};
	}
}
