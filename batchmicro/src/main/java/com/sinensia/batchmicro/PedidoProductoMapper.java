package com.sinensia.batchmicro;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class PedidoProductoMapper implements RowMapper<PedidoProductoDTO>  {

	@Override
	public PedidoProductoDTO mapRow(ResultSet rs, int rowNum) throws SQLException {

		PedidoProductoDTO detallePedidoDto = new PedidoProductoDTO();
		
		detallePedidoDto.setCodigoPedido(rs.getLong("CODIGO_PEDIDO"));
		detallePedidoDto.setCodigoProducto(rs.getLong("CODIGO_PRODUCTO"));
		detallePedidoDto.setCodigoProveedor(rs.getLong("CODIGO_PROVEEDOR"));
		detallePedidoDto.setNombre(rs.getString("nombre"));
		detallePedidoDto.setTipoProducto(rs.getString("tipoProducto"));
		detallePedidoDto.setCantidad(rs.getLong("cantidad"));
		detallePedidoDto.setPesoTotal(rs.getDouble("peso_total"));

		return detallePedidoDto;
	}
	
}
