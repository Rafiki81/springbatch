package com.sinensia.batchmicro;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.sinensia.batchmicro.config.AbstractJobConfig;



@Configuration
public class JobPedidoConfig extends AbstractJobConfig{
	
private static final String SELECT;
	
	static {
		SELECT = new StringBuilder()
				.append(" SELECT")
				.append("    pedidos.codigo_pedido,")
				.append("    productos.CODIGO_PRODUCTO,")
				.append("    productos.CODIGO_PROVEEDOR,")
				.append("    productos.nombre,")
				.append("    productos.tipoProducto,")
				.append("    detalle_pedidos.cantidad,")
				.append("    productos.peso *  detalle_pedidos.cantidad as peso_total")
				.append(" FROM")
				.append("    detalle_pedidos")
				.append("        JOIN")
				.append("    pedidos ON detalle_pedidos.CODIGO_PEDIDO = pedidos.CODIGO_PEDIDO")
				.append("        JOIN")
				.append("    productos ON detalle_pedidos.CODIGO_PRODUCTO = productos.CODIGO_PRODUCTO")
				.append("        AND detalle_pedidos.CODIGO_PROVEEDOR = productos.CODIGO_PROVEEDOR")
				.append(" WHERE")
				.append("    pedidos.fecha > date_sub(curdate(),interval 1 day)").toString();
	}
	
	@Bean
	@Qualifier("jobPedido")
	public Job jobPedido() {
		return jobBuilderFactory.get("jobPedido")
			.flow(step())
			.end()
			.build();
	}
	
	@Bean
	public Step step() {
		
		return stepBuilderFactory.get("step")
				.<PedidoProductoDTO,PedidoProductoDTO> chunk(10)
				.reader(reader())
				.writer(writer(null))
				.build();
	}
	
	@Bean(destroyMethod="")
	@StepScope
	public JdbcCursorItemReader<PedidoProductoDTO> reader() {
		
		System.out.println("fase de construcción del reader");
		
		JdbcCursorItemReader<PedidoProductoDTO> cursorItemReader = new JdbcCursorItemReader<>();
		cursorItemReader.setDataSource(dataSource);
		cursorItemReader.setSql(SELECT);
		cursorItemReader.setRowMapper(new PedidoProductoMapper());
		
		return cursorItemReader;
		
	}
	
	@Bean(destroyMethod = "")
	@StepScope
	public FlatFileItemWriter<PedidoProductoDTO> writer(@Value ("#{jobParameters['fecha']}") Date fecha){
		
		System.out.println("fase de construcción del writer");
		
		FlatFileItemWriter<PedidoProductoDTO> writer = new FlatFileItemWriter<>();
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");

		Calendar cal = Calendar.getInstance();
		//fecha = new Date();
		cal.setTime(fecha);
		cal.add(Calendar.DATE, -1);
		Date fechaAyer = cal.getTime();
		
		String fechaCsv = sdf.format(fechaAyer);
		
		writer.setResource(new FileSystemResource("materiales/salidas/pedidos_"+fechaCsv+".csv"));
		
		BeanWrapperFieldExtractor<PedidoProductoDTO> fieldExtractor = new BeanWrapperFieldExtractor<>();
		fieldExtractor.setNames(new String[] {
				"codigoPedido" 
				,"codigoProducto"
				,"codigoProveedor"
				,"nombre"
				,"tipoProducto"
				,"cantidad"
				,"pesoTotal"          
				});
		
		DelimitedLineAggregator<PedidoProductoDTO> lineAggregator = new DelimitedLineAggregator<>();
		
		lineAggregator.setDelimiter(",");
		lineAggregator.setFieldExtractor(fieldExtractor);
		
		writer.setLineAggregator(lineAggregator);
 		
		return writer;
	}

}
