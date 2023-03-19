package br.com.hfn.faturacartaocreditojob.reader;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

import br.com.hfn.faturacartaocreditojob.dominio.FaturaCartaoCredito;
import br.com.hfn.faturacartaocreditojob.dominio.Transacao;

public class FaturaCartaoCreditoReader implements ItemStreamReader<FaturaCartaoCredito>{

	private ItemStreamReader<Transacao> delegate;
	private Transacao transacaoAtual;
	
	public FaturaCartaoCreditoReader(ItemStreamReader<Transacao> delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		this.delegate.open(executionContext);
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		this.delegate.update(executionContext);
	}

	@Override
	public void close() throws ItemStreamException {
		this.delegate.close();
	}

	@Override
	public FaturaCartaoCredito read() throws Exception {
		if(null == this.transacaoAtual) {
			this.transacaoAtual = this.delegate.read();
		}
		
		FaturaCartaoCredito fatura = null;
		Transacao transacao = this.transacaoAtual;
		this.transacaoAtual = null;
		
		if(null!=transacao) {
			fatura = new FaturaCartaoCredito();
			fatura.setCartaoCredito(transacao.getCartaoCredito());
			fatura.setCliente(transacao.getCartaoCredito().getCliente());
			fatura.getTransacoes().add(transacao);
			
			while(isTransacaoRelacionada(transacao)) {
				fatura.getTransacoes().add(this.transacaoAtual);
			}
		}
		
		return fatura;
	}

	private boolean isTransacaoRelacionada(Transacao transacao) throws Exception {
		return null!=peek() && transacao.getCartaoCredito().getNumeroCartaoCredito()==this.transacaoAtual.getCartaoCredito().getNumeroCartaoCredito();
	}

	private Object peek() throws Exception {
		this.transacaoAtual = this.delegate.read();
		return this.transacaoAtual;
	}

}
