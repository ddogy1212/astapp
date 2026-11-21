#!/usr/bin/env python3
"""Optional NASA Images photo packaging for ASTAPP Android debug/release builds.

Fetches small archival photographic previews directly from NASA's public Images
API. The image file is only packed when NASA-sourced metadata is found and the
download is recognizable as JPEG. This step is best-effort: failures don't
fail the APK build and the app can fetch NASA imagery online when available.

Attribution catalog is generated alongside images. NASA photos are historical
observations and may represent composites, not real-time observations.
"""
import concurrent.futures
import json
from pathlib import Path
import re
import sys
import urllib.parse
import urllib.request
from urllib.error import URLError

OUTPUT=Path("app/src/main/assets/nasa")
OUTPUT.mkdir(parents=True,exist_ok=True)
SEARCH="https://images-api.nasa.gov/search"
TARGETS=[
    ("planet_0","Mercury planet MESSENGER","mercury"),
    ("planet_1","Venus planet Magellan","venus"),
    ("planet_2","Earth Blue Marble full globe","earth"),
    ("planet_3","Mars full disk Hubble","mars"),
    ("planet_4","Jupiter Juno planet","jupiter"),
    ("planet_5","Saturn Cassini rings planet","saturn"),
    ("planet_6","Uranus Voyager 2 planet","uranus"),
    ("planet_7","Neptune Voyager 2 planet","neptune"),
    ("moon","Moon full disk LRO","moon"),
    ("cosmic_0","Orion Nebula M42","orion"),
    ("cosmic_1","Andromeda Galaxy M31","andromeda"),
    ("cosmic_2","Pleiades M45","pleiades"),
    ("cosmic_3","Crab Nebula M1","crab"),
    ("cosmic_4","Eagle Nebula M16","eagle"),
    ("cosmic_5","Ring Nebula M57","ring"),
    ("cosmic_6","M13 globular cluster","globular"),
    ("cosmic_7","Whirlpool Galaxy M51","whirlpool")
]
# These NASA IDs were cross-checked against official photojournal entries.
# The originals are observations or mosaics, not artist impressions.
PREFERRED={
    "planet_0":("PIA16853","Mercury MESSENGER enhanced-color observation"),
    "planet_1":("PIA00072","Venus Galileo ultraviolet cloud observation"),
    "planet_3":("PIA00407","Mars Viking global color image mosaic"),
    "planet_4":("PIA04866","Jupiter Cassini true-color spacecraft mosaic"),
    "planet_5":("PIA01969","Saturn Voyager 2 archival rings photograph"),
    "planet_7":("PIA01492","Neptune Voyager 2 full-disk image"),
    "moon":("GSFC_20171208_Archive_e001982","NASA Clementine full Moon observation composite"),
    "cosmic_0":("PIA08653","Orion Nebula · Spitzer infrared observation"),
    "cosmic_1":("PIA26276","Andromeda Galaxy · Spitzer infrared observation"),
    "cosmic_2":("PIA08260","Pleiades star cluster · NASA observation"),
    "cosmic_3":("PIA21474","Crab Nebula · multi-observatory observation composite"),
    "cosmic_4":("PIA25433","Eagle Nebula · WISE infrared observation"),
    "cosmic_5":("PIA14443","Ring Nebula · NASA observation"),
    "cosmic_7":("PIA10200","Whirlpool Galaxy · NASA observation")
}
NO_ART=re.compile(r"artist|illustrat|simulation|computer.?generat|animation|diagram|infographic|painting|concept|render|graphic|movie poster",re.I)
USER_AGENT={"User-Agent":"ASTAPP/0.4 educational research (NASA imagery credit preserved)"}
ALLOWED_CENTERS={"JPL","JSC","GSFC","MSFC","ARC","KSC","HQ","AMES","STSCI","NASA"}
def get(url,maxbytes=2100000):
    req=urllib.request.Request(url,headers=USER_AGENT)
    with urllib.request.urlopen(req,timeout=12) as stream:
        blob=stream.read(maxbytes+1)
        if len(blob)>maxbytes:
            raise ValueError("image exceeds preview limit")
        return blob

def fetch(item):
    stem,query,keyword=item
    filepath=OUTPUT/(stem+".jpg")
    meta={}
    try:
        # M13: Hubble photo is provided by NASA Science as WebP. Android 8+
        # BitmapFactory decodes this format natively; do not fake a JPEG header.
        if stem=="cosmic_6":
            url="https://science.nasa.gov/wp-content/uploads/2023/04/m13-xlarge_web-jpg.webp"
            try:
                blob=get(url,2100000)
                if blob[:4]==b"RIFF" and blob[8:12]==b"WEBP":
                    (OUTPUT/"cosmic_6.webp").write_bytes(blob)
                    meta={"nasa_id":"",
                          "title":"Messier 13 · Hubble globular cluster observation",
                          "center":"NASA / ESA",
                          "credit":"NASA, ESA and Hubble Heritage Team (STScI/AURA)",
                          "details":"https://science.nasa.gov/image-detail/m13-xlarge_web/",
                          "size":len(blob)}
                    return (stem,meta,None)
            except Exception as ex:
                print("M13 NASA Hubble photo unavailable",str(ex)[:100],flush=True)
        if stem in PREFERRED:
            nasa_id,title=PREFERRED[stem]
            url=f"https://images-assets.nasa.gov/image/{nasa_id}/{nasa_id}~thumb.jpg"
            try:
                jpeg=get(url,2100000)
                if jpeg.startswith(b"\xff\xd8\xff"):
                    filepath.write_bytes(jpeg)
                    meta={"nasa_id":nasa_id,"title":title,"center":"NASA",
                          "credit":"NASA / archival science data",
                          "details":"https://images.nasa.gov/details/"+nasa_id,
                          "size":len(jpeg)}
                    return (stem,meta,None)
            except Exception as exception:
                print("CURATED IMAGE RETRY",stem,str(exception)[:90],flush=True)
        params=urllib.parse.urlencode({"q":query,"media_type":"image","page_size":40})
        body=json.loads(get(SEARCH+"?"+params,1600000))
        entries=body.get("collection",{}).get("items",[])
        for entry in entries:
            data=(entry.get("data") or [{}])[0]
            title=data.get("title","")
            desc=data.get("description","")
            center=(data.get("center") or "").upper()
            copyright_note=str(data.get("copyright","")).strip()
            if not keyword.lower() in (title+" "+desc).lower(): continue
            if NO_ART.search(title):continue
            if copyright_note and copyright_note.lower() not in ("nasa","public domain"):continue
            # Preserve NASA agency provenance and original metadata.
            if center not in ALLOWED_CENTERS:continue
            nasa_id=data.get("nasa_id")
            links=entry.get("links") or []
            href=(links[0].get("href") if links else "") or ""
            if not href.startswith("https://images-assets.nasa.gov/"):continue
            jpeg=get(href,2100000)
            if not jpeg.startswith(b"\xff\xd8\xff"):continue
            filepath.write_bytes(jpeg)
            meta={"nasa_id":nasa_id,"title":title,"center":center,
                  "credit":data.get("secondary_creator") or center,
                  "details":"https://images.nasa.gov/details/"+urllib.parse.quote(nasa_id or ""),
                  "size":len(jpeg)}
            return (stem,meta,None)
        return (stem,None,"no verified NASA photograph")
    except Exception as ex:
        return (stem,None,str(ex)[:130])

def main():
    credits={}
    with concurrent.futures.ThreadPoolExecutor(max_workers=5) as pool:
        jobs=[pool.submit(fetch,target) for target in TARGETS]
        for future in concurrent.futures.as_completed(jobs):
            name,info,error=future.result()
            if info:
                credits[name]=info
                print("BUNDLED",name,info["nasa_id"],info["size"],flush=True)
            else:
                print("SKIPPED",name,error,flush=True)
    (OUTPUT/"credits.json").write_text(json.dumps(credits,ensure_ascii=False,indent=2),encoding="utf-8")
    print(f"NASA offline photographs bundled: {len(credits)}/{len(TARGETS)}",flush=True)

if __name__=="__main__":
    main()
