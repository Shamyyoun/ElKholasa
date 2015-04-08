package json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import datamodels.ProviderObject;

public class ProvidersHandler {
	private String response;

	public ProvidersHandler(String response) {
		this.response = response;
	}

	public ArrayList<ProviderObject> handle() {
        ArrayList<ProviderObject> providers = new ArrayList<ProviderObject>();

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("providers");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                int id = jsonObject1.getInt("id");
                String name = jsonObject1.getString("name");

                ProviderObject providerObject = new ProviderObject();
                providerObject.setId(id);
                providerObject.setName(name);
                providers.add(providerObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
            providers = null;
        }

        return  providers;
	}
}
