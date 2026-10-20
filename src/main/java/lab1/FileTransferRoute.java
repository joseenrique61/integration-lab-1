package lab1;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

public class FileTransferRoute extends RouteBuilder {
  public static void main(String[] args) throws Exception {
    Main main = new Main();
    main.configure().addRoutesBuilder(new FileTransferRoute());
    main.run();
  }

  @Override
  public void configure() throws Exception {
    from("file:input?noop=true")
        .routeId("procesado")
        // Decidimos utilizar .choice() en vez de .filter() para poder hacer un log de
        // los archivos omitidos
        .choice()
          .when(header("CamelFileName").endsWith(".csv"))
            .log("Procesando archivo: ${file:name}")
            .convertBodyTo(String.class)
            .transform().simple("${body.toUpperCase()}")
            .to("file:output")

          .otherwise()
            .log("Omitiendo archivo con extensión '.${file:ext}': ${file:name}")
            // Guardar a un archivo en logs/
            .setBody(simple("[${date:now:yyyy-MM-dd HH:mm:ss}] ARCHIVO RECHAZADO: ${file:name} no tiene extensión .csv\\n", false, false))
            .to("file:logs?fileName=rechazados${date:now:yyyyMMdd}.log&fileExist=Append");

    from("file:output?noop=true")
        .routeId("archivado")
        .log("[${date:now:yyyy-MM-dd HH:mm:ss}] Archivando copia de: ${file:name}")
        .to("file:archive");
  }
}
