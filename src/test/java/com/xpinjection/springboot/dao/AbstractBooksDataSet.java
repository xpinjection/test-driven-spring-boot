package com.xpinjection.springboot.dao;

import com.github.database.rider.core.api.dataset.DataSetProvider;
import com.github.database.rider.core.dataset.builder.ColumnBuilder;
import com.github.database.rider.core.dataset.builder.DataSetBuilder;

public abstract class AbstractBooksDataSet implements DataSetProvider {
    protected ColumnBuilder createBook() {
        return new DataSetBuilder().table("book")
                .columns("id", "name", "author");
    }
}
