#!/usr/bin/env python3
import athenahealth
import betterdoctors
import geocoding

import aiohttp
import aiohttp.web
import aiohttp_cors
import aiohttp_index

import argparse
import datetime
import logging
import pprint
import sys

LOG = logging.getLogger(__name__)

api_bd = betterdoctors.API(
    'b6ad50fa4cd303cd314fd8e550cc1eb1'  # key
)

api_ah = athenahealth.API(
    'preview1',                 # version
    'nyj3n28af24szhvh5vuryzue', # key
    'EmZgbJ4C7ZeWP6H',          # secret
    195900                      # practiceid
)

api_gc = geocoding.API()

SPECIALTIES = {254914004:'nephrologist'}
DEFAULT_SPECIALTY = 'internist'

async def login(request: aiohttp.web.Request):
    data = await request.json()
    patientid = await api_ah.enhanced_best_match(
        data['firstname'],
        data['lastname'],
        datetime.date(data['dobyear'], data['dobmonth'], data['dobday']),
    )
    return aiohttp.web.json_response({'id': patientid})

async def results(request: aiohttp.web.Request):
    try:
        patientid = request.query['id']
    except KeyError:
        raise aiohttp.web.HTTPBadRequest(reason="Missing id parameter")

    patient = await api_ah.patient(patientid)
    LOG.info("Patient address: %s", str(patient.address))
    patient_coordinates = await api_gc.coordinates_of(patient.address)

    provider_specialty = SPECIALTIES[patient.diagnoses[0]] if patient.diagnoses else DEFAULT_SPECIALTY
    providers = await api_bd.search_doc(provider_specialty, patient_coordinates, 10, 'distance-asc', 3)
    LOG.info("Results: %s", pprint.pformat(results))
    patient_insurance = None if not patient.insurances else patient.insurances[0]
    for provider in providers:
        LOG.info("Insurances: %s vs %s", str([str(i) for i in patient.insurances]),str([str(i) for i in provider.insurances]))
    return aiohttp.web.json_response([
        {
            'id': 0,
            'name': provider.name,
            'address': str(provider.addresses[0]),
            'phone': provider.phones[0],
            'specialty': provider.specialties[0]
        }
        for provider in providers
        if not patient_insurance or patient_insurance in provider.insurances
    ])

async def test_ah(request: aiohttp.web.Request):
    data = await api_ah.patient(25275)
    return aiohttp.web.Response(text=pprint.pformat(data._data))

async def test_bd(request: aiohttp.web.Request):
    coordinates = await api_gc.coordinates_of(
        geocoding.Address("Google Launchpad", "San Francisco", "94105")
    )
    # coordinates = geocoding.Coordinates(37.789527, -122.394276)
    data = await api_bd.search_doc('ophthalmologist', coordinates, 10, 'distance-asc', 3)
    return aiohttp.web.Response(text=pprint.pformat([d._data for d in data]))

def main():
    parser = argparse.ArgumentParser(description="personalized health test")
    parser.add_argument('--port', type=int, default=3001)
    parser.add_argument("-v", "--verbose", help="increase output verbosity",
                        action="store_true")
    args = parser.parse_args()
    if args.verbose:
        logging.basicConfig(level=logging.DEBUG)

    app = aiohttp.web.Application(middlewares=[aiohttp_index.IndexMiddleware()])
    cors = aiohttp_cors.setup(app)
    cors.add(app.router.add_post('/login', login))
    cors.add(app.router.add_get('/results', results))
    app.router.add_get('/test_ah', test_ah)
    app.router.add_get('/test_bd', test_bd)
    app.router.add_static('/', 'ext/react_app/build')

    aiohttp.web.run_app(app, port=args.port)
    api_ah.close()
    api_bd.close()
    return 0

if __name__ == '__main__':
    sys.exit(main())
