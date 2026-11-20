use("oliveconnect");

const collections = [
  "utilisateurs", "vergers", "varietes", "recoltes", "tournees",   //M1
  "alertes", "notifications",                                      //M2
  "ouvriers", "tracteurs", "bennes", "materiels", "affectations",  //M3
  "pressoirs", "receptions", "releves_maturite", "meteo"           //M4
];

collections.forEach(name => {
  if (!db.getCollectionNames().includes(name)) {
    db.createCollection(name);
  }
});

print("Collections: " + db.getCollectionNames().join(", "));