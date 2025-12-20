package com.tiancai.dto;

import lombok.Data;

@Data
public class BookDetailDTO {
    private Integer bookId;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private Integer publicationYear;
    private String coverImageUrl;
}
