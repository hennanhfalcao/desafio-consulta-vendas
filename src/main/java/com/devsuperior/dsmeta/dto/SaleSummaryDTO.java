package com.devsuperior.dsmeta.dto;

import java.time.LocalDate;

public class SaleSummaryDTO {

    private String sellerName;
    private Double total;

    public SaleSummaryDTO(String sellerName, Double total) {
        this.sellerName = sellerName;
        this.total = total;
    }

    public SaleSummaryDTO(){}

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }
}
