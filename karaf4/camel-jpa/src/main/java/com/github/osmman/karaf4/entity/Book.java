package com.github.osmman.karaf4.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@Entity(name = "Book")
@Table(name = "books")
@XmlRootElement(name = "book")
@XmlAccessorType(XmlAccessType.FIELD)
@NamedQuery(name = "getAllBooks", query = "SELECT b FROM Book b")
//@NamedNativeQuery(name = "getAllBooks", query = "SELECT * FROM books")
public class Book implements Serializable {

	private static final long serialVersionUID = -6379243362687684144L;

	@Id
	@TableGenerator(name = "appSeqStore", initialValue = 1, allocationSize = 1,
			table = "ID_GEN", pkColumnName = "GEN_KEY", valueColumnName = "GEN_VALUE", pkColumnValue = "BOOK_ID")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "appSeqStore")
	private Long id;

	private String name;

	public Book() {
	}

	public Book(final String name) {
		this.name = name;
	}

	public Book(final Long id, final String name) {
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Book{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}
}
