package com.example.account.repository;

import com.example.account.dto.account.request.AccountListRequestDTO;
import com.example.account.model.account.AccountEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Specifications for querying {@link AccountEntity} with dynamic filters.
 */
public final class AccountSpecifications {

    private AccountSpecifications() {
        // utility
    }

    public static Specification<AccountEntity> withFilters(AccountListRequestDTO request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request == null) {
                return cb.conjunction();
            }

            if (StringUtils.hasText(request.getAccountName())) {
                String like = "%" + request.getAccountName().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get(AccountEntity.FIELD_ACCOUNT_NAME)), like));
            }

            if (StringUtils.hasText(request.getAccountId())) {
                predicates.add(cb.equal(root.get(AccountEntity.FIELD_ACCOUNT_ID), request.getAccountId()));
            }

            if (StringUtils.hasText(request.getCurrency())) {
                predicates.add(cb.equal(root.get(AccountEntity.FIELD_CURRENCY), request.getCurrency()));
            }

            if (StringUtils.hasText(request.getCountryCode())) {
                predicates.add(cb.equal(root.get(AccountEntity.FIELD_COUNTRY_CODE), request.getCountryCode()));
            }

            if (StringUtils.hasText(request.getCity())) {
                String like = "%" + request.getCity().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get(AccountEntity.FIELD_CITY)), like));
            }

            if (StringUtils.hasText(request.getState())) {
                predicates.add(cb.equal(root.get(AccountEntity.FIELD_STATE), request.getState()));
            }

            if (StringUtils.hasText(request.getZipcode())) {
                predicates.add(cb.equal(root.get(AccountEntity.FIELD_ZIPCODE), request.getZipcode()));
            }

            if (StringUtils.hasText(request.getStatus())) {
                predicates.add(cb.equal(root.get(BaseEntity.FIELD_STATUS), request.getStatus()));
            }

            return predicates.isEmpty()
                    ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

