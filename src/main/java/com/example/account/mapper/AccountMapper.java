package com.example.account.mapper;

import com.example.account.dto.account.response.AccountCreateResponseDTO;
import com.example.account.dto.account.response.AccountDeleteResponseDTO;
import com.example.account.dto.account.response.AccountDetailsResponseDTO;
import com.example.account.dto.account.response.AccountUpdateResponseDTO;
import com.example.account.model.account.AccountEntity;
import com.example.account.util.AccountMessages;
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
     *
     * @param entity Account entity
     * @return AccountDetailsResponseDTO
     */
    AccountDetailsResponseDTO toDetailsDTO(AccountEntity entity);

    /**
     * Convert AccountEntity to AccountCreateResponseDTO.
     *
     * @param entity Account entity
     * @return AccountCreateResponseDTO
     */
    AccountCreateResponseDTO toCreateResponseDTO(AccountEntity entity);

    /**
     * Convert AccountEntity to AccountUpdateResponseDTO.
     *
     * @param entity Account entity
     * @return AccountUpdateResponseDTO
     */
    AccountUpdateResponseDTO toUpdateResponseDTO(AccountEntity entity);

    /**
     * Convert AccountEntity to AccountDeleteResponseDTO with fixed message and deleted flag.
     *
     * @param entity Account entity
     * @return AccountDeleteResponseDTO
     */
    @Mapping(target = "message", expression = "java(AccountMessages.ACCOUNT_DELETED_SUCCESSFULLY)")
    @Mapping(target = "deleted", constant = "true")
    AccountDeleteResponseDTO toDeleteResponseDTO(AccountEntity entity);
}


