package br.com.felipemira.arquivos.office;

import java.awt.AWTException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import br.com.felipemira.arquivos.office.custom.document.CustomXWPFDocument;
import br.com.felipemira.arquivos.office.iterator.WordIterator;
import br.com.felipemira.convert.ConvertToPDF;
import br.com.felipemira.copy.Copy;
import br.com.felipemira.objects.object.CasoDeTeste;

public class WordAutomacao {
	
	private String nomeArquivo;
	private String nomeComHora;
	private String horaExecucao;
	private Calendar horaInicio;
	private String caminhoTemplate;
	private String caminhoImagem;
	private String caminhoDoc;
	private int countFalhou = 0;
	private CustomXWPFDocument document;
	
	/**
	 * Cria um documento Word atrav�s do template.
	 * @param nomeArquivo - String.
	 * @param caminhoDoc - String.
	 * @param caminhoTemplate - String.
	 * @param caminhoImagem - String.
	 */
	public WordAutomacao(String nomeArquivo, String caminhoDoc, String caminhoTemplate, String caminhoImagem, CasoDeTeste casoDeTeste){
		
		this.nomeArquivo = nomeArquivo;
		this.caminhoDoc = caminhoDoc;
		this.caminhoTemplate = caminhoTemplate;
		this.caminhoImagem = caminhoImagem;
		
		try {
			criarDocumento(casoDeTeste);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Pega o templete da evid�ncia e cria um novo documento word.
	 * @throws IOException
	 */
	private void criarDocumento(CasoDeTeste casoDeTeste) throws IOException{
		//Pega a hora que foi criado o documento.
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        this.horaInicio = cal;
        this.horaExecucao = dateFormat.format(cal.getTime());
        String date = this.horaExecucao.replaceAll("/", "_").replaceAll(" ", "_").replaceAll(":", "_");
        
        //Implementa o nome do arquivo de evidencias que sera criado.
        this.nomeComHora = nomeArquivo + "_" + date;
        
        //Defino para toda a classe o caminho da minha evidencia
        this.caminhoDoc = this.caminhoDoc + this.nomeComHora + ".docx";
        
        
		CustomXWPFDocument document = null;
		
        //Crio um file com o template.
        File file = new File(this.caminhoTemplate);
        
        //Faco uma copia do template renomeada com o nome da minha evidencia
        if(file.exists()) {
        	File copiaFile = new File(caminhoDoc);
        	Copy.copyFile(file, copiaFile);
        	InputStream arquivo = new FileInputStream(caminhoDoc);
			CustomXWPFDocument copiaDocumento = new CustomXWPFDocument(arquivo);
	        document = copiaDocumento;
	        this.document = document;
        }else{
        	System.out.println("templateEvidencia.docx n�o consta na pasta informada!");
        }
        
        //Insere o nome do CT na tabela um, primeira linha da primeira coluna.
        //WordIterator.inserirDadoTabela(this.caminhoDoc, document, 0, 0, 0, this.nomeArquivo);
        //Insere a hora de execu��o na tabela um, primeira linha da segunda coluna.
        //WordIterator.inserirDadoTabela(this.caminhoDoc, document, 0, 0, 1, this.horaExecucao);
        
        //Insere o t�tulo no documento word.
        //WordIterator.inserirTitulo(this.caminhoDoc, document, this.nomeArquivo);
        
        WordIterator.inserirDadoTabela(this.caminhoDoc, document, 0, 0, 0, casoDeTeste.getItemDeReferencia());
        WordIterator.inserirDadoTabela(this.caminhoDoc, document, 0, 1, 3, this.horaExecucao.substring(0, 10));
        WordIterator.inserirDadoTabela(this.caminhoDoc, document, 0, 2, 1, casoDeTeste.getSiglaCasoDeTeste() + " - " + casoDeTeste.getIdCasoDeTeste());
        WordIterator.inserirDadoTabela(this.caminhoDoc, document, 0, 3, 1, casoDeTeste.getCenarioDeTeste());
        WordIterator.inserirDadoTabela(this.caminhoDoc, document, 0, 4, 1, casoDeTeste.getCasoDeTesteCondicao());
        WordIterator.inserirDadoTabela(this.caminhoDoc, document, 0, 6, 1, casoDeTeste.getNomeDoTestador());
	}
	
	
	/**
	 * Insere uma evid�ncia no arquivo Word. Para gerar imagem � necess�rio passar true no par�metro imagem.
	 * @param mensagem - String
	 * @param passouFalhou - Boolean
	 * @param imagem - Boolean
	 */
	public void inserirEvidencia(String mensagem, Boolean passouFalhou, Boolean imagem){
		if(passouFalhou){
			mensagem = mensagem + " - Passou";
		}else{
			mensagem = mensagem + " - Falhou";
			this.countFalhou = 1;
		}
		try {
			gerarEvidencia(mensagem, imagem);
		} catch (InvalidFormatException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (AWTException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Gera uma evidencia e insere no arquivo Word.
	 * @param mensagem - String
	 * @param geraImagem - Boolean
	 * @throws IOException
	 * @throws AWTException
	 * @throws InvalidFormatException
	 */
	private void gerarEvidencia(String mensagem, Boolean geraImagem) throws IOException, AWTException, InvalidFormatException{
	    if(geraImagem){
	    	WordIterator.inserirTexto(this.caminhoDoc, this.document, mensagem);
	    	WordIterator.inserirImagem(this.caminhoDoc, this.document, this.caminhoImagem);
	    }else{
	    	WordIterator.inserirTexto(this.caminhoDoc, this.document, mensagem);
	    }
	}
	
	/**
	 * Calcula a dura��o da execu��o.
	 * @return String.
	 */
	private String calcularDuracao(){
        
        long diferenca = System.currentTimeMillis() - this.horaInicio.getTimeInMillis();
        long diferencaHoras = diferenca / (60 * 60 * 1000);
        long diferencaMin = (diferenca % (60 * 60 * 1000))/ (60 * 1000);
        long diferencaSeg = (diferenca % (60 * 60 * 1000))% (60 * 1000) / 1000;
        
        String horas = "";
        String minutos = "";
        String segundos = "";
        
        if(String.valueOf(diferencaHoras).length() == 1){
        	horas = "0" + String.valueOf(diferencaHoras);
        }else{
        	horas = String.valueOf(diferencaHoras);
        }
        
        if(String.valueOf(diferencaMin).length() == 1){
        	minutos = "0" + String.valueOf(diferencaMin);
        }else{
        	minutos = String.valueOf(diferencaMin);
        }
        
        if(String.valueOf(diferencaSeg).length() == 1){
        	segundos = "0" + String.valueOf(diferencaSeg);
        }else{
        	segundos = String.valueOf(diferencaSeg);
        }
        
        
        return horas + ":" + minutos + ":" + segundos;
	}
	
	/**
	 * Finaliza a evid�ncia e cria o arquivo PDF
	 * @throws IOException
	 */
	public void finalizarEvidencia() throws IOException{
	    
	    //Insere a dura��o do CT na tabela um, segunda linha, segunda coluna.
	    WordIterator.inserirDadoTabela(this.caminhoDoc, this.document, 0, 1, 1, calcularDuracao());
	    
	    //Insere o Status do caso de teste na tabela.
	    if(this.countFalhou == 1){
	    	WordIterator.inserirDadoTabela(this.caminhoDoc, this.document, 0, 1, 0, "Falhou");
	    }else{
	    	WordIterator.inserirDadoTabela(this.caminhoDoc, this.document, 0, 1, 0, "Passou");
	    }
	    
	    ConvertToPDF.convert(this.caminhoDoc, this.nomeComHora);
	}
	
	/**
	 * Insere dados em uma tabela especificada pelo n�mero.
	 * @param caminhoDoc - String com o caminho.
	 * @param documento - CustomXWPFDocument.
	 * @param numeroTabela - int - Come�a com 0.
	 * @param numeroLinha - int - come�a com 0.
	 * @param numeroColuna - int - come�a com 0.
	 * @param dado - String a ser inserida.
	 * @throws IOException
	 */
	public void inserirDadoTabela(int numeroTabela, int numeroLinha, int numeroColuna, String dado){
		try {
			WordIterator.inserirDadoTabela(this.caminhoDoc, this.document, numeroTabela, numeroLinha, numeroColuna, dado);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
