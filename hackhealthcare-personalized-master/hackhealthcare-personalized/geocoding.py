import aiohttp

class Address(object):
    def __init__(self, i_street:str, i_city:str, i_zip:str):
        self._street = i_street
        self._city = i_city
        self._zip = i_zip

    def __str__(self):
        return "{}, {}, {}".format(
            self._street,
            self._city,
            self._zip,
        )

class Coordinates(object):
    def __init__(self, i_lat: float, i_lon: float):
        self._lat = i_lat
        self._lon = i_lon

    def __str__(self):
        return "{}, {}".format(
            self._lat,
            self._lon,
        )

    @property
    def lat(self):
        return self._lat

    @property
    def lon(self):
        return self._lon

class API(object):
    BASE_URI = 'http://maps.google.com/maps/api/geocode/json'
    
    def __init__(self):
        self._connection = aiohttp.ClientSession() 

    def close(self):
        self._connection.close()

    async def coordinates_of(self, i_address: Address) -> Coordinates:
        params = {
            'address': str(i_address),
        }
        response = await self._connection.get(self.BASE_URI, params=params)
        response_json = await response.json()
        response_location = response_json['results'][0]['geometry']['location']

        return Coordinates(response_location['lat'], response_location['lng'])
