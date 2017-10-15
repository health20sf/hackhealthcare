import geocoding

import aiohttp

from typing import List

class InsuranceType(object):
    HMO = 1
    PPO = 2
    UNKNOWN = 3

class InsurancePlan(object):
    def __init__(self, i_name: str):
        self._name = i_name.split(' ')[0].lower()
        self._type = InsuranceType.UNKNOWN

    def __eq__(self, i_other) -> bool:
        return self._name == i_other._name and self._type == i_other._type

    def __str__(self):
        return "({}, {})".format(self._name, self._type)

class Provider(object):
    def __init__(self, i_data:dict):
        self._data = i_data

    @property
    def name(self) -> str:
        return "{} {}, {}".format(
                self._data['profile']['first_name'],
                self._data['profile']['last_name'],
                self._data['profile']['title']
        )

    @property
    def languages(self) -> List[str]:
        return [lang['name'] for lang in self._data['profile']['languages']]

    @property
    def addresses(self) -> List[geocoding.Address]:
        return [
            geocoding.Address(
                loc['visit_address']['street'],
                loc['visit_address']['city'],
                loc['visit_address']['zip']
            )
            for loc in self._data['practices']
        ]

    @property
    def phones(self) -> List[str]:
        return [
            loc['phones'][0]['number']
            for loc in self._data['practices']
        ]

    @property
    def insurances(self) -> List[InsurancePlan]:
        return [
            InsurancePlan(
                i['insurance_plan']['name']
            )
            for i in self._data['insurances']
        ]

    @property
    def sex(self) -> str:
        return self._data['profile']['gender']

    @property
    def specialties(self) -> List[str]:
        return [sp['name'] for sp in self._data['specialties']]

class API(object):
    BASE_URL = "https://api.betterdoctor.com/2016-03-01/doctors"

    def __init__(self, i_api_key: str):
        self._api_key = i_api_key or ""
        self._connection = aiohttp.ClientSession()

    def close(self):
        self._connection.close()

    async def search_doc(self, i_specialty: str, i_coord: geocoding.Coordinates, i_radius_miles: float, i_sorting_method: str, i_max_results: int):
        params = {
            'limit': i_max_results,
            'location': "{},{},{}".format(i_coord.lat, i_coord.lon, i_radius_miles),
            'skip': 0,
            'sort': i_sorting_method,
            'specialty_uid': i_specialty,
            'user_key': self._api_key,
            'user_location': "{},{}".format(i_coord.lat, i_coord.lon),
        }
        response = await self._connection.get(self.BASE_URL, params=params)
        response_json = await response.json()
        results = [
            Provider(datum)
            for datum in response_json['data']
        ]

        return results
