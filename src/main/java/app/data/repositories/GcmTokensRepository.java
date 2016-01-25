package app.data.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entities.GcmTokens;

public interface GcmTokensRepository extends JpaRepository<GcmTokens, Integer> {
	
	@Override
	public GcmTokens findOne(Integer id);
	
	public GcmTokens findByNumber(String number);
	
	public List<GcmTokens> findAllByNumber(String number);
	
	public GcmTokens findByToken(String token);

}
