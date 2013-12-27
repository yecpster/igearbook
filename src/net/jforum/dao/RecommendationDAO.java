package net.jforum.dao;

import java.util.List;

import com.igearbook.entities.PaginationData;
import com.igearbook.entities.PaginationParams;
import com.igearbook.entities.Recommendation;

public interface RecommendationDAO {
    public Recommendation selectById(int id);

    public void delete(int id);

    public void update(Recommendation recommendation);

    public int addNew(Recommendation recommendation);

    public PaginationData<Recommendation> selectAllByLimit(PaginationParams params);

    public List<Recommendation> selectByTypeByLimit(int type, int startFrom, int count);

    public Recommendation selectByTopicId(int topicId);
}
