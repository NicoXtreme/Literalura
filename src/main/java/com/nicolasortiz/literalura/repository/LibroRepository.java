package com.nicolasortiz.literalura.repository;

import com.nicolasortiz.literalura.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    Libro findByTitulo(String titulo);
    List<Libro> findByIdiomasContaining(String idioma);
}
