import api from './client';

export const recommendationService = {
    getRecommendations() {
        return api.get('/recommendations');
    }
};