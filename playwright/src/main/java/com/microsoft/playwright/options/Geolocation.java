/*
 * Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.options;

public class Geolocation {
  public double latitude;
  public double longitude;
  public Double accuracy;

  public Geolocation() {
  }

  public Geolocation(double latitude, double longitude) {
    this(latitude, longitude, null);
  }

  public Geolocation(double latitude, double longitude, Double accuracy) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.accuracy = accuracy;
  }

  public Geolocation withLatitude(double latitude) {
    this.latitude = latitude;
    return this;
  }
  public Geolocation withLongitude(double longitude) {
    this.longitude = longitude;
    return this;
  }
  public Geolocation withAccuracy(double accuracy) {
    this.accuracy = accuracy;
    return this;
  }
}
