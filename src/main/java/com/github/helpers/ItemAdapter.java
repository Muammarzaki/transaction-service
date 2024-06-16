package com.github.helpers;

import com.github.domain.TransactionDomain;
import com.github.entities.ItemEntity;

import java.util.List;
import java.util.stream.Collectors;

public abstract class ItemAdapter {
	private ItemAdapter() {
	}

	public static TransactionDomain.ItemsDomain convertFromListOfItemEntityToItemsDomain(List<ItemEntity> items) {
		if (items == null) throw new IllegalArgumentException("Item Entity null");
		return items.stream().map(ItemAdapter::convertFromItemEntityToItemDomain).collect(Collectors.toCollection(TransactionDomain.ItemsDomain::new));
	}

	public static List<ItemEntity> convertFromListOfItemEntityToItemEntity(TransactionDomain.ItemsDomain domain) {
		if (domain == null) throw new IllegalArgumentException("Items Domain null");
		return domain.stream().map(itm -> ItemEntity.builder()
				.itemId(itm.itemId())
				.itemName(itm.itemName())
				.quantity(itm.quantity())
				.price(itm.price())
				.build())
			.toList();
	}

	public static TransactionDomain.ItemsDomain.ItemDomain convertFromItemEntityToItemDomain(ItemEntity entity) {
		if (entity == null) throw new IllegalArgumentException("Item entity null");
		return new TransactionDomain.ItemsDomain.ItemDomain(entity.getItemId(), entity.getItemName(), entity.getQuantity(), entity.getPrice());
	}

	public static ItemEntity convertFromListOfItemEntityToItemEntity(TransactionDomain.ItemsDomain.ItemDomain domain) {
		if (domain == null) throw new IllegalArgumentException("Item domain null");
		return ItemEntity.builder()
			.itemId(domain.itemId())
			.itemName(domain.itemName())
			.price(domain.price())
			.quantity(domain.quantity())
			.build();
	}
}
