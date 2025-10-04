package Green_trade.green_trade_platform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = false)
    private String name;

    @Column(name = "quantity", nullable = false, unique = false)
    private Long quantity;

    @Column(name = "category_id", nullable = false, unique = false)
    private Long categoryId;

    @Column(name = "description", nullable = true, unique = false)
    private String description;

    @Column(name = "brand", nullable = false, unique = false)
    private String brand;

    @Column(name = "model", nullable = false, unique = false)
    private String model;

    private int year;

}
