import requests
import json

def search_doc(specialty, lat, long, radius, sorting, max_results, api_key):
    base_url = "https://api.betterdoctor.com/2016-03-01/doctors?"
    doc_loc = "location=" + str(lat) + "%2C" + str(long) + "%2C" + str(radius)
    user_loc = "user_location=" + str(lat) + "%2C" + str(long)
    spec = "specialty_uid=" + specialty
    sort = "sort=" + sorting
    res_lim = "limit=" + str(max_results)
    user_key = "user_key=" + api_key
    req_url = base_url + spec + "&" + doc_loc + "&" + user_loc + "&" + sort + "&skip=0&" + res_lim + "&" + user_key
    r = requests.get(req_url)
    count = len(r.json()["data"])
    results = []
    data = r.json()["data"]
    for c in range(count):
        out = {}
        out["name"] = data[c]["profile"]["first_name"] + " " + data[c]["profile"]["last_name"] + ", " + data[c]["profile"]["title"]
        out["sex"] = data[c]["profile"]["gender"]
        out["languages"] = [lang["name"] for lang in data[c]["profile"]["languages"]]
        out["specialties"] = [sp["name"] for sp in data[c]["specialties"]]
        out["locations"] = [loc["visit_address"]["street"] + ", " + loc["visit_address"]["city"] + ", " + loc["visit_address"]["zip"] for loc in data[c]["practices"]]
        out["plans"] = [i["insurance_plan"]["name"] for i in data[c]["insurances"]]
        results.append(out)
        return results

# search_doc("ophthalmologist", 42.340079, -71.109298, 10, "distance-asc", 3, "ENTER BD API KEY")