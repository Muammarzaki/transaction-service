package com.github.domain;

import java.util.LinkedList;

public class ItemsDomain extends LinkedList<ItemsDomain.ItemDomain> {

	public int getTotalPrice(){
		return this.parallelStream()
			.mapToInt(x -> x.count*x.price)
			.sum();
	}


	public record ItemDomain(
		String itemId,
		String itemName,
		int count,
		int price

	) {
	}
}
