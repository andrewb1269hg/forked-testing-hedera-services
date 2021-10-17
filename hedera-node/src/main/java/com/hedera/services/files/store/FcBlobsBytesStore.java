package com.hedera.services.files.store;

/*-
 * ‌
 * Hedera Services Node
 * ​
 * Copyright (C) 2018 - 2021 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

import com.hedera.services.state.merkle.MerkleBlob;
import com.hedera.services.state.merkle.internals.BlobKey;
import com.swirlds.merkle.map.MerkleMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.AbstractMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static com.hedera.services.state.merkle.internals.BlobKey.typeFromCharCode;
import static java.lang.Long.parseLong;

public class FcBlobsBytesStore extends AbstractMap<String, byte[]> {
	private static final Logger log = LogManager.getLogger(FcBlobsBytesStore.class);
	private final Supplier<MerkleMap<BlobKey, MerkleBlob>> blobSupplier;

	public static final int LEGACY_BLOB_CODE_INDEX = 3;

	public FcBlobsBytesStore(Supplier<MerkleMap<BlobKey, MerkleBlob>> blobSupplier) {
		this.blobSupplier = blobSupplier;
	}

	/**
	 * The string we are parsing has one of five special forms:
	 * <ul>
	 *    <li>{@literal /0/f{num}} for file data; or,</li>
	 *    <li>{@literal /0/k{num}} for file metadata; or,</li>
	 *    <li>{@literal /0/s{num}} for contract bytecode; or,</li>
	 *    <li>{@literal /0/d{num}} for contract storage; or,</li>
	 *    <li>{@literal /0/e{num}} for prior expiration time of a system-deleted entity.</li>
	 * </ul>
	 * So we get the type from the character code at index 3, and parse the entity number
	 * starting at index 4, to get the appropriate {@link BlobKey}.
	 *
	 * @param path a string with one of the five forms above
	 * @return a fixed-size map key with equivalent meaning
	 */
	BlobKey at(Object path) {
		final String s = (String) path;
		final BlobKey.BlobType type = typeFromCharCode(s.charAt(LEGACY_BLOB_CODE_INDEX));
		final long entityNum = getEntityNumFromPath(s);
		return new BlobKey(type, entityNum);
	}

	@Override
	public void clear() {
		blobSupplier.get().clear();
	}

	/**
	 * Removes the blob at the given path.
	 *
	 * <B>NOTE:</B> This method breaks the standard {@code Map} contract,
	 * and does not return the contents of the removed blob.
	 *
	 * @param path
	 * 		the path of the blob
	 * @return {@code null}
	 */
	@Override
	public byte[] remove(Object path) {
		blobSupplier.get().remove(at(path));
		return null;
	}

	/**
	 * Replaces the blob at the given path with the given contents.
	 *
	 * <B>NOTE:</B> This method breaks the standard {@code Map} contract,
	 * and does not return the contents of the previous blob.
	 *
	 * @param path
	 * 		the path of the blob
	 * @param value
	 * 		the contents to be set
	 * @return {@code null}
	 */
	@Override
	public byte[] put(String path, byte[] value) {
		var meta = at(path);
		if (blobSupplier.get().containsKey(meta)) {
			final var blob = blobSupplier.get().getForModify(meta);
			blob.setData(value);
			if (log.isDebugEnabled()) {
				log.debug("Modifying to {} new bytes (hash = {}) @ '{}'", value.length, blob.getHash(), path);
			}
		} else {
			final MerkleBlob blob = new MerkleBlob(value);
			if (log.isDebugEnabled()) {
				log.debug("Putting {} new bytes (hash = {}) @ '{}'", value.length, blob.getHash(), path);
			}
			blobSupplier.get().put(at(path), blob);
		}
		return null;
	}

	@Override
	public byte[] get(Object path) {
		return Optional.ofNullable(blobSupplier.get().get(at(path)))
				.map(MerkleBlob::getData)
				.orElse(null);
	}

	@Override
	public boolean containsKey(Object path) {
		return blobSupplier.get().containsKey(at(path));
	}

	@Override
	public boolean isEmpty() {
		return blobSupplier.get().isEmpty();
	}

	@Override
	public int size() {
		return blobSupplier.get().size();
	}

	@Override
	public Set<Entry<String, byte[]>> entrySet() {
		throw new UnsupportedOperationException();
	}

	/**
	 * As the string we are parsing matches /0/f{num} for file data, /0/k{num} for file metadata, /0/s{num} for contract
	 * bytecode, and /0/e{num} for system deleted files, character at third position is used to recognize the type of
	 * blob
	 *
	 * @param key
	 * 		given blob key
	 * @return
	 */
	public static long getEntityNumFromPath(final String key) {
		return parseLong(key.substring(LEGACY_BLOB_CODE_INDEX + 1));
	}
}
