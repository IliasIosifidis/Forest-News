// digest-service writes one each evening, summing up the articles of the past 24 hours.
export interface Digest {
  /** The day it covers, as `YYYY-MM-DD`. */
  day: string
  /** One story per line. */
  content: string
  createdAt: string
}
