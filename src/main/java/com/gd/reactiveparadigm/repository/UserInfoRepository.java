package com.gd.reactiveparadigm.repository;

import com.gd.reactiveparadigm.domain.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends ReactiveMongoRepository<User, String> {

}
