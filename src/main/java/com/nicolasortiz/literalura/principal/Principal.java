package com.nicolasortiz.literalura.principal;

import com.nicolasortiz.literalura.model.*;
import com.nicolasortiz.literalura.repository.AutorRepository;
import com.nicolasortiz.literalura.repository.LibroRepository;
import com.nicolasortiz.literalura.service.ConsumoAPI;
import com.nicolasortiz.literalura.service.ConvierteDatos;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Principal {
    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private Scanner teclado = new Scanner(System.in);

    private LibroRepository libroRepository;
    private AutorRepository autorRepository;

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void muestraElMenu(){
        var json = consumoAPI.obtenerDatos(URL_BASE);
        var opcion = -1;
        while (opcion !=0) {
            var menu =  """
                    \n******** Menú ********
                    
                    1 - Buscar libro por título en Gutendex
                    2 - Listar libros registrados en la DB
                    3 - Listar autores registrados en la DB
                    4 - Listar autores vivos antes de un determinado año
                    5 - Listar libros por idioma registrados en la base de datos
                    6 - Listar los 5 libros más descargados registrado en la base de datos
                    7 - Estadísticas
                    
                    0 - Salir
                    
                    Opción: 
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarLibroPorTitulo();
                    break;
                case 2:
                    listarLibrosRegistrados();
                    break;
                case 3:
                    listarAutoresRegistrados();
                    break;
                case 4:
                    listarAutoresVivosPorAnio();
                    break;
                case 5:
                    listarLibrosPorIdioma();
                    break;
                case 6:
                    listarLibrosMasDescargados();
                    break;
                case 7:
                    generarEstadisticas();
                    break;
                case 0:
                    System.out.println("\nCerrando la aplicación...\n");
                    break;
                default:
                    System.out.println("\nOpción inválida\n");
            }
        }
    }

    private Libro crearLibro(DatosLibros datoslibros, Autor autor){
        if (autor != null){
            return new Libro(datoslibros, autor);
        } else {
            System.out.println("El autor es nulo, no es posible crear el libro");
            return null;
        }
    }


    //Consumo desde la API de Gutendex
    private void buscarLibroPorTitulo() {
        System.out.println("Escribe el nombre del libro que deseas buscar");
        var nombreLibro = teclado.nextLine();
        if (!nombreLibro.isBlank()){
            var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + nombreLibro.replace(" ", "+"));
            var datos = conversor.obtenerDatos(json, Datos.class);
            if (!datos.resultados().isEmpty()){
                DatosLibros datosLibros = datos.resultados().get(0);
                DatosAutor datosAutor = datosLibros.autor().get(0);
                Libro libro = null;
                Libro libroRepo = libroRepository.findByTitulo(datosLibros.titulo());
                if (libroRepo != null){
                    System.out.println("\nEste libre ya se encuentra ingresado en la base de datos.");
                    System.out.println(libroRepo.toString());
                } else {
                    Autor autorRepo = autorRepository.findByNombreIgnoreCase(datosLibros.autor().get(0).nombre());
                    if (autorRepo != null){
                        libro = crearLibro(datosLibros, autorRepo);
                        libroRepository.save(libro);
                        System.out.println("\nSe agregó un nuevo libro a la base de datos. \n");
                        System.out.println(libro);
                    } else {
                        Autor autor = new Autor(datosAutor);
                        autor = autorRepository.save(autor);
                        libro = crearLibro(datosLibros, autor);
                        libroRepository.save(libro);
                        System.out.println("\nSe agregó un nuevo libro a la base de datos. \n");
                        System.out.println(libro);
                    }
                }
            } else {
                System.out.println("\n<ATENCION> El libro buscado NO existe en la API de Gutendex, ingresa otro");
            }
        } else {
            System.out.println("No ingresó un nombre de libro.");
        }


    }

    //Consumo desde la DB db_literalura
    private void listarLibrosRegistrados() {
        System.out.println("Los libros registrados en la base de datos son: \n");
        List<Libro> librosRegistrados = libroRepository.findAll();
        if (!librosRegistrados.isEmpty()){
            librosRegistrados.stream()
                    .forEach(System.out::println);
        } else {
            System.out.println("No hay ningún libro aún registrado en la base de datos.");
        }
    }

    private void listarAutoresRegistrados() {
        System.out.println("\nLos autores registrados en la base de datos son: \n");
        List<Autor> autoresRegistrados = autorRepository.findAll();
        if (!autoresRegistrados.isEmpty()){
            autoresRegistrados.stream()
                    .sorted(Comparator.comparing(Autor::getNombre))
                    .forEach(System.out::println);
        } else {
            System.out.println("No hay ningún autor aún registrado en la base de datos.");
        }
    }

    private void listarAutoresVivosPorAnio() {
        System.out.println("Ingresa el año para listar los autores que deseas buscar");
        var anio = teclado.nextInt();
        teclado.nextLine();
        if (anio > 0) {
            List<Autor> autorPorAnio = autorRepository
                    .findByFechaDeNacimientoLessThanEqualAndFechaDeMuerteGreaterThanEqual(anio, anio);
            if (!autorPorAnio.isEmpty()){
                System.out.println("Los autores vivos registrados en el año " + anio + " en la base de datos son: \n");
                autorPorAnio.stream()
                        .sorted(Comparator.comparing(Autor::getNombre))
                        .forEach(System.out::println);
            } else {
                System.out.println("No hay ningún autor que estuviese vivio en el año " + anio + " registrado en la base de datos.");
            }
        } else {
            System.out.println("Debes ingresar una fecha válida");
        }
    }

    private void listarLibrosPorIdioma() {
        System.out.println("Escribe el idioma del libro que deseas buscar. Utilice las siguientes opciones que estan entre []:");
        System.out.println("""
                [ES]: español
                [EN]: inglés
                [FR]: francés
                [PT]: portugués
                [IT]: italiano
                """);
        var idiomaBuscado = teclado.nextLine().toLowerCase();
        if (!idiomaBuscado.isBlank()){
            if (idiomaBuscado.equals("es") ||
                    idiomaBuscado.equals("en") ||
                    idiomaBuscado.equals("fr") ||
                    idiomaBuscado.equals("pt") ||
                    idiomaBuscado.equals("it")
            ){
                List<Libro> librosBuscados = libroRepository.findByIdiomasContaining(idiomaBuscado);
                if (!librosBuscados.isEmpty()){
                    AtomicInteger contador = new AtomicInteger(0);
                    librosBuscados.stream()
                            .sorted(Comparator.comparing(Libro::toString))
                            .forEach(libro -> {
                                System.out.println(libro);
                                contador.incrementAndGet();
                            });
                    System.out.println("\n Estan registrados " + contador + " libros en " + "[" + idiomaBuscado + "]");
                } else {
                    System.out.println("No hay ningún libro con el idioma " + idiomaBuscado + " registrado en la base de datos");
                }
            } else {
                System.out.println("Ingresó un idioma inválido");
            }
        } else {
            System.out.println("No escribió ningún idioma.");
        }
    }

    private void listarLibrosMasDescargados() {
        System.out.println("Los libros descargados registrados en la base de datos son: \n");
        List<Libro> librosMasDescargados = libroRepository.findAll();
        librosMasDescargados.stream()
                .sorted(Comparator.comparing(Libro::getNumeroDeDescargas).reversed())
                .limit(5)
                .forEach(System.out::println);
    }

    private void generarEstadisticas() {
        System.out.println("""
                            \nEstadísticas de la base de datos
                            --------------------------------
                           """);
        List<Libro> libro = libroRepository.findAll();
        if (!libro.isEmpty()) {
            DoubleSummaryStatistics estadisticas = libro.stream()
                    .collect(Collectors.summarizingDouble(Libro::getNumeroDeDescargas));
            System.out.println("\nLibro más descargado: " + estadisticas.getMax());
            System.out.println("Libro menos descargado: " + estadisticas.getMin());
            System.out.printf("Promedio de descargas: %.2f%n ", estadisticas.getAverage());
        } else {
            System.out.println("No hay libros registrados en la base de datos");
        }
    }
}
