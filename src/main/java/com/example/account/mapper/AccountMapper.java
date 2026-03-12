package com.example.account.mapper;

import com.example.account.dto.account.response.AccountDetailsResponseDTO;
import com.example.account.model.account.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for converting Account entities to DTOs.
 */
@Mapper
public interface AccountMapper {

    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    /**
     * Convert AccountEntity to AccountDetailsResponseDTO.
     * Maps accountId field directly (no mapping needed as field names match).
     * 
     * @param entity Account entity
     * @return AccountDetailsResponseDTO
     */
    AccountDetailsResponseDTO toDetailsDTO(AccountEntity entity);
}

