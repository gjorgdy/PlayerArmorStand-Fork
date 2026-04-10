package com.danrus.pas.api.data;

import com.danrus.pas.api.info.NameInfo;

import java.util.HashMap;
import java.util.Set;

/**
 * DataManager interface for managing data sources and retrieving SkinData.
 * This interface allows adding data sources, retrieving data by player name,
 * and invalidating data when necessary.
 */

public interface DataRepository<T extends DataHolder> {

    /**
     * Adds a source of data to the manager.
     * The source must implement DataCache interface.
     *
     * @param source the data source to add
     */
    void addSource(DataProvider<T> source);

    /**
     * Add a source of data to the manager with priority.
     *
     * @param source the data source to add
     * @param priority the priority of the source, higher values indicate higher priority
     */
    void addSource(DataProvider<T> source, int priority);

    /**
     * Retrieves data associated with the given string.
     *
     * @param info the NameInfo of the player
     * @return SkinData associated with the identifier, or null if not found
     */
    T getData(NameInfo info);

    /**
     * Retrieves all data available in the manager.
     *
     * @param info the NameInfo of the player
     * @param data Object data to be stored
     */
    void store(NameInfo info, T data);
    void store(DataStoreKey key, T data);

    /**
     * Invalidates the data associated with the given name.
     * This method should be called when the data is no longer valid or needs to be refreshed.
     *
     * @param info the identifier for the data to invalidate
     */
    void invalidateData(NameInfo info);

    /**
     * Retrieves a specific data source by its key.
     *
     * @param key the key of the data source
     * @return the DataCache associated with the key, or null if not found
     */

    DataProvider<T> getSource(String key);

    /**
     * Retrieves all data sources managed by this DataManager.
     * @return a HashMap containing all data sources, where the key is the source key and the value is the DataCache
     */

    HashMap<String, DataProvider<T>> getSources();


    /**
     * Retrieves all data stored in {@link CacheSkinData}
     *
     * @return a HashMap containing all game data, where the key is the player name and the value is SkinData
     */

    /**
     * Finds SkinData by a given string without download.
     *
     * @param info the NameInfo for the skin data
     * @return SkinData associated with the identifier, or null if not found
     */

    T findData(NameInfo info);


    /**
     * Deletes the data associated with the given string.
     *
     * @param info the NameInfo for the data to delete
     */

    void delete(NameInfo info);

    Set<DataStoreKey> keySet();
}
