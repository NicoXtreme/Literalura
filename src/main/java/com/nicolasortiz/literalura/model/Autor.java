package com.nicolasortiz.literalura.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "autores")
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String nombre;
    private Integer fechaDeNacimiento;
    private Integer fechaDeMuerte;

    //Un autor puede tener varios libros
    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Libro> libro = new ArrayList<>();

    public Autor(){

    }

    public Autor(DatosAutor authorData) {
        this.nombre = authorData.nombre();
        this.fechaDeNacimiento = authorData.fechaDeNacimiento();
        this.fechaDeMuerte = authorData.fechaDeMuerte();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getFechaDeNacimiento() {
        return fechaDeNacimiento;
    }

    public void setFechaDeNacimiento(Integer fechaDeNacimiento) {
        this.fechaDeNacimiento = fechaDeNacimiento;
    }

    public Integer getFechaDeMuerte() {
        return fechaDeMuerte;
    }

    public void setFechaDeMuerte(Integer fechaDeMuerte) {
        this.fechaDeMuerte = fechaDeMuerte;
    }

    public List<Libro> getLibro() {
        return libro;
    }

    public void setLibro(List<Libro> libro) {
        this.libro = libro;
    }

    @Override
    public String toString() {
        StringBuilder titulos = new StringBuilder();
        for (Libro libro: libro) {
            titulos.append(libro.getTitulo()).append(", ");
        }

        // Eliminar la última coma y espacio
        if (titulos.length() > 0) {
            titulos.setLength(titulos.length() - 2);
        }


        return "Autor{" +
                "nombre='" + nombre + '\'' + "\n" +
                ", fechaDeNacimiento=" + fechaDeNacimiento + "\n" +
                ", fechaDeMuerte=" + fechaDeMuerte + "\n" +
                ", libros=" + titulos + "\n" +
                '}';
    }
}
