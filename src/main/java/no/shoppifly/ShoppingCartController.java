package no.shoppifly;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
public class ShoppingCartController implements ApplicationListener<ApplicationReadyEvent> {
	private final CartService cartService;

	private final MeterRegistry meterRegistry;

	@Autowired
	public ShoppingCartController(
			CartService cartService,
			MeterRegistry meterRegistry
	) {
		this.cartService = cartService;
		this.meterRegistry = meterRegistry;
	}

	@GetMapping(path = "/cart/{id}")
	public Cart getCart(@PathVariable String id) {
		return cartService.getCart(id);
	}

	/**
	 * Checks out a shopping cart. Removes the cart, and returns an order ID
	 *
	 * @return an order ID
	 */
	@Timed
	@PostMapping(path = "/cart/checkout")
	public String checkout(@RequestBody Cart cart) {
		return cartService.checkout(cart);
	}

	/**
	 * Updates a shopping cart, replacing it's contents if it already exists. If no cart exists (id is null)
	 * a new cart is created.
	 *
	 * @return the updated cart
	 */
	@PostMapping(path = "/cart")
	public Cart updateCart(@RequestBody Cart cart) {
		return cartService.update(cart);
	}

	/**
	 * return all cart IDs
	 *
	 * @return
	 */
	@GetMapping(path = "/carts")
	public List<String> getAllCarts() {
		return cartService.getAllsCarts();
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
		Gauge.builder(
			"carts",
				cartService,
				service -> service.getAllsCarts().size()
			)
			.register(meterRegistry);

		Gauge.builder(
				"cartsvalue",
				cartService,
				CartService::total
		)
		.register(meterRegistry);

		Gauge.builder(
				"checkouts",
				cartService,
				CartService::getNumShoppingCartsCheckedOut
		)
		.register(meterRegistry);
	}
}