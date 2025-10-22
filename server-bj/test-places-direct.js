const axios = require('axios');

const GOOGLE_MAPS_API_KEY = process.env.GOOGLE_MAPS_API_KEY || 'AIzaSyBTyxYYZLqhTjFt7OqxO5RXsryYaL2uAPE';

async function testAutocomplete() {
  console.log('Testing with API Key:', GOOGLE_MAPS_API_KEY.substring(0, 10) + '...');
  
  try {
    const url = 'https://maps.googleapis.com/maps/api/place/autocomplete/json';
    const params = {
      input: 'Av. Reforma',
      key: GOOGLE_MAPS_API_KEY,
      components: 'country:mx',
      language: 'es',
    };

    console.log('Calling:', url);
    const response = await axios.get(url, { params });
    
    console.log('Status:', response.data.status);
    console.log('Predictions:', response.data.predictions?.length || 0);
    console.log('Full response:', JSON.stringify(response.data, null, 2));
  } catch (error) {
    console.error('Error:', error.message);
    if (error.response?.data) {
      console.error('Response data:', error.response.data);
    }
  }
}

testAutocomplete();
