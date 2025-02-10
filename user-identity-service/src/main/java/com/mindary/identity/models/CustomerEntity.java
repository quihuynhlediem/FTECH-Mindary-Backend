package com.mindary.identity.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "customers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id"})})
@Data
@AllArgsConstructor
@SuperBuilder
public class CustomerEntity extends User {
}
