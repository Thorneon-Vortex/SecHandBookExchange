package com.tiancai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    private Integer bookId;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String publicationYear;
    private String coverImageUrl;
}
