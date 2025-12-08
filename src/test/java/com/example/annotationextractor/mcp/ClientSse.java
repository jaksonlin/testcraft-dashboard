package com.example.annotationextractor.mcp;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import com.example.annotationextractor.mcp.SampleClient;

/**
 * @author Christian Tzolov
 */
public class ClientSse {

	public static void main(String[] args) {
		
		HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder("http://localhost:8090").build();

		new SampleClient(transport).run();
	}

}